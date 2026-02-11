import path from "node:path";
import process from "node:process";
import { homedir } from "node:os";
import { mkdir, readFile, writeFile } from "node:fs/promises";
import type { CliArgs, Provider } from "./types";

function printUsage(): void {
  console.log(`用法：
  npx -y bun scripts/main.ts --prompt "一只猫" --image cat.png
  npx -y bun scripts/main.ts --prompt "一幅风景画" --image landscape.png --ar 16:9
  npx -y bun scripts/main.ts --promptfiles system.md content.md --image out.png

选项：
  -p, --prompt <text>       提示词文本
  --promptfiles <files...>  从文件读取提示词（拼接）
  --image <path>            输出图像路径（必需）
  --provider google|openai  强制指定服务商（默认自动检测）
  -m, --model <id>          模型 ID
  --ar <ratio>              宽高比（例如 16:9、1:1、4:3）
  --size <WxH>              尺寸（例如 1024x1024）
  --quality normal|2k       质量预设（默认：2k）
  --imageSize 1K|2K|4K      Google 图像尺寸（默认：由质量决定）
  --ref <files...>          参考图像（仅 Google 多模态）
  --n <count>               生成数量（默认：1）
  --json                    JSON 输出
  -h, --help                显示帮助

环境变量：
  OPENAI_API_KEY            OpenAI API 密钥
  GOOGLE_API_KEY            Google API 密钥
  GEMINI_API_KEY            Gemini API 密钥（GOOGLE_API_KEY 的别名）
  OPENAI_IMAGE_MODEL        默认 OpenAI 模型 (gpt-image-1.5)
  GOOGLE_IMAGE_MODEL        默认 Google 模型 (gemini-3-pro-image-preview)
  OPENAI_BASE_URL           自定义 OpenAI 端点
  GOOGLE_BASE_URL           自定义 Google 端点

环境文件加载顺序：命令行参数 > process.env > <cwd>/.baoyu-skills/.env > ~/.baoyu-skills/.env`);
}

function parseArgs(argv: string[]): CliArgs {
  const out: CliArgs = {
    prompt: null,
    promptFiles: [],
    imagePath: null,
    provider: null,
    model: null,
    aspectRatio: null,
    size: null,
    quality: "2k",
    imageSize: null,
    referenceImages: [],
    n: 1,
    json: false,
    help: false,
  };

  const positional: string[] = [];

  const takeMany = (i: number): { items: string[]; next: number } => {
    const items: string[] = [];
    let j = i + 1;
    while (j < argv.length) {
      const v = argv[j]!;
      if (v.startsWith("-")) break;
      items.push(v);
      j++;
    }
    return { items, next: j - 1 };
  };

  for (let i = 0; i < argv.length; i++) {
    const a = argv[i]!;

    if (a === "--help" || a === "-h") {
      out.help = true;
      continue;
    }

    if (a === "--json") {
      out.json = true;
      continue;
    }

    if (a === "--prompt" || a === "-p") {
      const v = argv[++i];
      if (!v) throw new Error(`缺少 ${a} 的值`);
      out.prompt = v;
      continue;
    }

    if (a === "--promptfiles") {
      const { items, next } = takeMany(i);
      if (items.length === 0) throw new Error("缺少 --promptfiles 的文件");
      out.promptFiles.push(...items);
      i = next;
      continue;
    }

    if (a === "--image") {
      const v = argv[++i];
      if (!v) throw new Error("缺少 --image 的值");
      out.imagePath = v;
      continue;
    }

    if (a === "--provider") {
      const v = argv[++i];
      if (v !== "google" && v !== "openai") throw new Error(`无效的服务商：${v}`);
      out.provider = v;
      continue;
    }

    if (a === "--model" || a === "-m") {
      const v = argv[++i];
      if (!v) throw new Error(`缺少 ${a} 的值`);
      out.model = v;
      continue;
    }

    if (a === "--ar") {
      const v = argv[++i];
      if (!v) throw new Error("缺少 --ar 的值");
      out.aspectRatio = v;
      continue;
    }

    if (a === "--size") {
      const v = argv[++i];
      if (!v) throw new Error("缺少 --size 的值");
      out.size = v;
      continue;
    }

    if (a === "--quality") {
      const v = argv[++i];
      if (v !== "normal" && v !== "2k") throw new Error(`无效的质量值：${v}`);
      out.quality = v;
      continue;
    }

    if (a === "--imageSize") {
      const v = argv[++i]?.toUpperCase();
      if (v !== "1K" && v !== "2K" && v !== "4K") throw new Error(`无效的图像尺寸：${v}`);
      out.imageSize = v;
      continue;
    }

    if (a === "--ref" || a === "--reference") {
      const { items, next } = takeMany(i);
      if (items.length === 0) throw new Error(`缺少 ${a} 的文件`);
      out.referenceImages.push(...items);
      i = next;
      continue;
    }

    if (a === "--n") {
      const v = argv[++i];
      if (!v) throw new Error("缺少 --n 的值");
      out.n = parseInt(v, 10);
      if (isNaN(out.n) || out.n < 1) throw new Error(`无效的数量：${v}`);
      continue;
    }

    if (a.startsWith("-")) {
      throw new Error(`未知选项：${a}`);
    }

    positional.push(a);
  }

  if (!out.prompt && out.promptFiles.length === 0 && positional.length > 0) {
    out.prompt = positional.join(" ");
  }

  return out;
}

async function loadEnvFile(p: string): Promise<Record<string, string>> {
  try {
    const content = await readFile(p, "utf8");
    const env: Record<string, string> = {};
    for (const line of content.split("\n")) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith("#")) continue;
      const idx = trimmed.indexOf("=");
      if (idx === -1) continue;
      const key = trimmed.slice(0, idx).trim();
      let val = trimmed.slice(idx + 1).trim();
      if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) {
        val = val.slice(1, -1);
      }
      env[key] = val;
    }
    return env;
  } catch {
    return {};
  }
}

async function loadEnv(): Promise<void> {
  const home = homedir();
  const cwd = process.cwd();

  const homeEnv = await loadEnvFile(path.join(home, ".baoyu-skills", ".env"));
  const cwdEnv = await loadEnvFile(path.join(cwd, ".baoyu-skills", ".env"));

  for (const [k, v] of Object.entries(homeEnv)) {
    if (!process.env[k]) process.env[k] = v;
  }
  for (const [k, v] of Object.entries(cwdEnv)) {
    if (!process.env[k]) process.env[k] = v;
  }
}

async function readPromptFromFiles(files: string[]): Promise<string> {
  const parts: string[] = [];
  for (const f of files) {
    parts.push(await readFile(f, "utf8"));
  }
  return parts.join("\n\n");
}

async function readPromptFromStdin(): Promise<string | null> {
  if (process.stdin.isTTY) return null;
  try {
    const t = await Bun.stdin.text();
    const v = t.trim();
    return v.length > 0 ? v : null;
  } catch {
    return null;
  }
}

function normalizeOutputImagePath(p: string): string {
  const full = path.resolve(p);
  const ext = path.extname(full);
  if (ext) return full;
  return `${full}.png`;
}

function detectProvider(args: CliArgs): Provider {
  if (args.provider) return args.provider;

  const hasGoogle = !!(process.env.GOOGLE_API_KEY || process.env.GEMINI_API_KEY);
  const hasOpenai = !!process.env.OPENAI_API_KEY;

  if (hasGoogle && !hasOpenai) return "google";
  if (hasOpenai && !hasGoogle) return "openai";
  if (hasGoogle && hasOpenai) return "google";

  throw new Error(
    "未找到 API 密钥。请设置 GOOGLE_API_KEY、GEMINI_API_KEY 或 OPENAI_API_KEY。\n" +
      "在 ~/.baoyu-skills/.env 或 <cwd>/.baoyu-skills/.env 中创建密钥文件。"
  );
}

type ProviderModule = {
  getDefaultModel: () => string;
  generateImage: (prompt: string, model: string, args: CliArgs) => Promise<Uint8Array>;
};

async function loadProviderModule(provider: Provider): Promise<ProviderModule> {
  if (provider === "google") {
    return (await import("./providers/google")) as ProviderModule;
  }
  return (await import("./providers/openai")) as ProviderModule;
}

async function main(): Promise<void> {
  const args = parseArgs(process.argv.slice(2));

  if (args.help) {
    printUsage();
    return;
  }

  await loadEnv();

  let prompt: string | null = args.prompt;
  if (!prompt && args.promptFiles.length > 0) prompt = await readPromptFromFiles(args.promptFiles);
  if (!prompt) prompt = await readPromptFromStdin();

  if (!prompt) {
    console.error("错误：提示词为必填项");
    printUsage();
    process.exitCode = 1;
    return;
  }

  if (!args.imagePath) {
    console.error("错误：--image 为必填项");
    printUsage();
    process.exitCode = 1;
    return;
  }

  const provider = detectProvider(args);
  const providerModule = await loadProviderModule(provider);
  const model = args.model || providerModule.getDefaultModel();
  const outputPath = normalizeOutputImagePath(args.imagePath);

  let imageData: Uint8Array;
  let retried = false;

  while (true) {
    try {
      imageData = await providerModule.generateImage(prompt, model, args);
      break;
    } catch (e) {
      if (!retried) {
        retried = true;
        console.error("生成失败，正在重试...");
        continue;
      }
      throw e;
    }
  }

  const dir = path.dirname(outputPath);
  await mkdir(dir, { recursive: true });
  await writeFile(outputPath, imageData);

  if (args.json) {
    console.log(
      JSON.stringify(
        {
          savedImage: outputPath,
          provider,
          model,
          prompt: prompt.slice(0, 200),
        },
        null,
        2
      )
    );
  } else {
    console.log(outputPath);
  }
}

main().catch((e) => {
  const msg = e instanceof Error ? e.message : String(e);
  console.error(msg);
  process.exit(1);
});
