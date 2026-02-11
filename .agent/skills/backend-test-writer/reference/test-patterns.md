# 测试模式参考

每个类别提供一个完整示例。其他 CRUD 操作遵循相同模式。

**前置准备：** 参阅 [test-setup.md](test-setup.md) 了解 `beforeAll/afterAll/afterEach` 样板代码。

---

## 路由/控制器（集成测试）

```javascript
const request = require('supertest');
const app = require('../app');
// 前置准备：参阅 test-setup.md 了解数据库连接样板代码

describe('POST /api/users', () => {
  describe('success', () => {
    it('should create and return 201', async () => {
      const res = await request(app)
        .post('/api/users')
        .send({ name: 'John', email: 'john@example.com', password: 'Pass123!' })
        .expect('Content-Type', /json/)
        .expect(201);

      expect(res.body).toHaveProperty('id');
      expect(res.body.email).toBe('john@example.com');
      expect(res.body).not.toHaveProperty('password'); // no leak
    });
  });

  describe('validation errors', () => {
    it('should return 400 for missing fields', async () => {
      const res = await request(app)
        .post('/api/users')
        .send({ name: 'John' }) // missing email, password
        .expect(400);
      expect(res.body).toHaveProperty('errors');
    });

    it('should return 400 for invalid email', async () => {
      await request(app)
        .post('/api/users')
        .send({ name: 'John', email: 'invalid', password: 'Pass123!' })
        .expect(400);
    });
  });

  describe('edge cases', () => {
    it('should return 409 for duplicate', async () => {
      const data = { name: 'John', email: 'john@example.com', password: 'Pass123!' };
      await request(app).post('/api/users').send(data);
      await request(app).post('/api/users').send(data).expect(409);
    });
  });
});

// GET /api/users/:id - 相同模式：成功 (200)、未找到 (404)、无效 id (400)
// PUT /api/users/:id - 相同模式 + 验证错误
// DELETE /api/users/:id - 相同模式，成功时期望 204
```

**其他 CRUD 操作遵循相同结构：**

| 方法 | 成功 | 未找到 | 无效输入 |
|------|------|--------|----------|
| GET /:id | 200 + body | 404 | 400（错误的 ObjectId） |
| PUT /:id | 200 + 已更新 | 404 | 400（验证） |
| DELETE /:id | 204 | 404 | 400（错误的 ObjectId） |

---

## 受保护路由

```javascript
describe('Protected Routes', () => {
  let token;

  beforeAll(async () => {
    // 登录获取 token
    const res = await request(app)
      .post('/api/auth/login')
      .send({ email: 'test@example.com', password: 'Pass123!' });
    token = res.body.token;
  });

  it('should return 401 without token', async () => {
    await request(app).get('/api/profile').expect(401);
  });

  it('should return 401 with invalid token', async () => {
    await request(app)
      .get('/api/profile')
      .set('Authorization', 'Bearer invalid')
      .expect(401);
  });

  it('should return 200 with valid token', async () => {
    await request(app)
      .get('/api/profile')
      .set('Authorization', `Bearer ${token}`)
      .expect(200);
  });

  // RBAC：相同模式，但角色不足时期望 403
});
```

---

## 服务层（单元测试）

```javascript
describe('UserService', () => {
  let service, mockRepo, mockEmail;

  beforeEach(() => {
    jest.clearAllMocks();
    mockRepo = {
      create: jest.fn(),
      findById: jest.fn(),
      findByEmail: jest.fn(),
    };
    mockEmail = { sendWelcome: jest.fn() };
    service = new UserService(mockRepo, mockEmail);
  });

  describe('createUser', () => {
    const data = { name: 'John', email: 'john@example.com', password: 'Pass123!' };

    it('should create user', async () => {
      mockRepo.findByEmail.mockResolvedValue(null);
      mockRepo.create.mockResolvedValue({ id: '1', ...data });

      const result = await service.createUser(data);

      expect(mockRepo.findByEmail).toHaveBeenCalledWith(data.email);
      expect(mockRepo.create).toHaveBeenCalled();
      expect(result).toHaveProperty('id');
    });

    it('should throw on duplicate', async () => {
      mockRepo.findByEmail.mockResolvedValue({ id: '1' });
      await expect(service.createUser(data)).rejects.toThrow('Email already exists');
      expect(mockRepo.create).not.toHaveBeenCalled();
    });

    it('should continue if email service fails', async () => {
      mockRepo.findByEmail.mockResolvedValue(null);
      mockRepo.create.mockResolvedValue({ id: '1' });
      mockEmail.sendWelcome.mockRejectedValue(new Error('SMTP'));

      const result = await service.createUser(data);
      expect(result).toHaveProperty('id'); // doesn't throw
    });
  });

  // getUserById：找到时使用 mockResolvedValue，未找到时返回 null → 抛出异常
  // updateUser：先检查是否存在，然后更新
  // deleteUser：相同模式
});
```

---

## 模型层（单元测试）

```javascript
describe('User Model', () => {
  describe('validation', () => {
    it('should require email', async () => {
      const user = new User({ name: 'John' });
      await expect(user.validate()).rejects.toThrow(/email/i);
    });

    it('should reject invalid email', async () => {
      const user = new User({ name: 'John', email: 'invalid', password: 'Pass!' });
      await expect(user.validate()).rejects.toThrow(/email/i);
    });

    it('should accept valid data', async () => {
      const user = new User({ name: 'John', email: 'john@example.com', password: 'Pass123!' });
      await expect(user.validate()).resolves.not.toThrow();
    });

    // 类似的：必填字段、枚举值、最小/最大长度
  });

  describe('methods', () => {
    it('should compare password', async () => {
      const user = await User.create({ name: 'John', email: 'j@e.com', password: 'Pass!' });
      expect(await user.comparePassword('Pass!')).toBe(true);
      expect(await user.comparePassword('wrong')).toBe(false);
    });
  });

  describe('toJSON', () => {
    it('should exclude password', () => {
      const user = new User({ name: 'John', email: 'j@e.com', password: 'hash' });
      expect(user.toJSON()).not.toHaveProperty('password');
    });
  });
});
```

---

## 中间件（单元测试）

```javascript
describe('Auth Middleware', () => {
  let req, res, next;

  beforeEach(() => {
    req = { headers: {} };
    res = { status: jest.fn().mockReturnThis(), json: jest.fn() };
    next = jest.fn();
  });

  it('should 401 without header', async () => {
    await authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
    expect(next).not.toHaveBeenCalled();
  });

  it('should 401 with invalid token', async () => {
    req.headers.authorization = 'Bearer invalid';
    await authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
  });

  it('should call next with valid token', async () => {
    const token = jwt.sign({ userId: '1' }, process.env.JWT_SECRET);
    req.headers.authorization = `Bearer ${token}`;
    await authMiddleware(req, res, next);
    expect(next).toHaveBeenCalled();
    expect(req.user).toBeDefined();
  });
});

// 验证中间件：相同的 mock 模式，检查 req.body
// 错误处理器：测试不同错误类型（ValidationError、CastError、11000）
```

---

## 工具函数（单元测试）

```javascript
describe('utils', () => {
  describe('slugify', () => {
    it.each([
      ['Hello World', 'hello-world'],
      ['foo bar baz', 'foo-bar-baz'],
      ['Hello! @World#', 'hello-world'],
      ['', ''],
    ])('slugify(%s) → %s', (input, expected) => {
      expect(slugify(input)).toBe(expected);
    });
  });

  describe('calculateDiscount', () => {
    it('should calculate correctly', () => {
      expect(calculateDiscount(100, 10)).toBe(90);
    });

    it('should throw for invalid input', () => {
      expect(() => calculateDiscount(-100, 10)).toThrow();
      expect(() => calculateDiscount(100, 150)).toThrow();
    });
  });
});
```

## 边缘情况模式

```javascript
// 输入验证 - 使用 it.each
it.each([
  [null, 'null'],
  [undefined, 'undefined'],
  ['', 'empty'],
  ['<script>', 'XSS'],
])('should reject %s (%s)', async (input) => {
  await request(app).post('/api/x').send({ field: input }).expect(400);
});

// 并发操作 - 竞态条件测试
it('should handle duplicate race condition', async () => {
  const email = 'race@test.com';
  const results = await Promise.allSettled([
    request(app).post('/api/users').send({ name: 'A', email, password: 'P1!' }),
    request(app).post('/api/users').send({ name: 'B', email, password: 'P2!' }),
  ]);
  const created = results.filter(r => r.value?.status === 201);
  const rejected = results.filter(r => r.value?.status === 409);
  expect(created).toHaveLength(1);
  expect(rejected).toHaveLength(1);
});
```
