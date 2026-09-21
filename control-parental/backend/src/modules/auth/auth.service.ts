import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { randomBytes } from 'crypto';
import * as bcrypt from 'bcrypt';

/**
 * Vinculación dispositivo-familia: el padre genera un código de invitación
 * de un solo uso desde su app; el dispositivo del menor lo canjea una vez
 * durante el onboarding (pantalla que explica qué se va a compartir).
 */
@Injectable()
export class AuthService {
  constructor(private readonly jwt: JwtService) {}

  generateInviteCode(): string {
    return randomBytes(4).toString('hex').toUpperCase(); // p. ej. "A1B2C3D4"
  }

  async validatePassword(plain: string, hash: string): Promise<boolean> {
    return bcrypt.compare(plain, hash);
  }

  async hashPassword(plain: string): Promise<string> {
    return bcrypt.hash(plain, 12);
  }

  issueTokens(userId: string, familyId: string, role: 'PARENT' | 'CHILD') {
    const accessToken = this.jwt.sign({ sub: userId, familyId, role });
    const refreshToken = this.jwt.sign(
      { sub: userId, type: 'refresh' },
      { expiresIn: '30d' },
    );
    return { accessToken, refreshToken };
  }

  verifyAccessToken(token: string) {
    try {
      return this.jwt.verify(token);
    } catch {
      throw new UnauthorizedException('Token inválido o expirado');
    }
  }
}
