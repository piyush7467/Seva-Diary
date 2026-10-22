const {
  hashPassword,
  verifyPassword,
  generateRandomToken,
  hashToken,
  signAccessToken,
  verifyAccessToken
} = require('../../src/common/utils/token.utils');

describe('Token & Cryptography Utilities', () => {
  it('should hash a password and verify correctly', async () => {
    const raw = 'SatSahib@123';
    const hash = await hashPassword(raw);
    expect(hash).not.toBe(raw);
    const valid = await verifyPassword(raw, hash);
    expect(valid).toBe(true);

    const wrong = await verifyPassword('WrongPassword', hash);
    expect(wrong).toBe(false);
  });

  it('should generate random hex token of correct length', () => {
    const token = generateRandomToken(32);
    expect(token).toBeDefined();
    expect(token.length).toBe(64); // 32 bytes = 64 hex characters
  });

  it('should deterministically SHA-256 hash raw tokens', () => {
    const raw = 'random-token-123';
    const hash1 = hashToken(raw);
    const hash2 = hashToken(raw);
    expect(hash1).toBe(hash2);
    expect(hash1.length).toBe(64);
  });

  it('should sign and verify access token with claims', () => {
    const payload = { sub: 'user_123', username: 'ramdas', displayName: 'Ram Das' };
    const token = signAccessToken(payload);
    expect(token).toBeDefined();

    const decoded = verifyAccessToken(token);
    expect(decoded.sub).toBe('user_123');
    expect(decoded.username).toBe('ramdas');
    expect(decoded.displayName).toBe('Ram Das');
  });
});
