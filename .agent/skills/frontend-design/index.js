// 示例文件：plugins/frontend-design/index.js
// 根据你的应用程序插件加载器 API 调整函数和签名。

module.exports = {
    name: 'frontend-design',
    version: '1.0.0',
    // 初始化函数（可选）
    init: async function (opts) {
      console.log('frontend-design 插件已初始化', opts || {});
      // 如需要，在此初始化依赖
    },
  
    // 加载器可调用的主函数。根据你的系统调整名称/签名。
    handle: async function (input) {
      console.log('frontend-design handle 被调用，参数：', input);
      // 最小响应示例
      return {
        ok: true,
        message: 'frontend-design 插件运行正常（请替换为实际逻辑）'
      };
    }
  };