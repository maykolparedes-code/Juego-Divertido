import { Body, Controller, Post } from '@nestjs/common';
import { IsString, MinLength } from 'class-validator';
import { AuthService } from './auth.service';

class RedeemInviteDto {
  @IsString()
  @MinLength(8)
  inviteCode!: string;

  @IsString()
  deviceModel!: string;

  @IsString()
  pushToken!: string;
}

@Controller('auth')
export class AuthController {
  constructor(private readonly auth: AuthService) {}

  /**
   * Canjea el código de invitación generado por el padre y crea el
   * vínculo dispositivo-familia. Este es el único paso de "emparejamiento":
   * no hay instalación silenciosa ni activación remota sin este código,
   * que el menor ve y acepta durante el onboarding.
   */
  @Post('redeem-invite')
  async redeemInvite(@Body() dto: RedeemInviteDto) {
    // Implementación real: buscar Family por inviteCode, crear Device,
    // marcar el código como usado, emitir tokens.
    return { message: 'Dispositivo vinculado', deviceModel: dto.deviceModel };
  }
}
