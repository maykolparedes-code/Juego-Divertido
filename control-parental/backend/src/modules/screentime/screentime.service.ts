import { Injectable, NotFoundException } from '@nestjs/common';
import { AppCategory } from '@prisma/client';
import { PrismaService } from '../../common/prisma/prisma.service';
import { AlertsService } from '../alerts/alerts.service';

interface CreateRuleInput {
  familyId: string;
  deviceId?: string;
  appPackage?: string;
  category?: AppCategory;
  dailyLimitMinutes: number;
  scheduleStart?: string;
  scheduleEnd?: string;
}

@Injectable()
export class ScreenTimeService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly alerts: AlertsService,
  ) {}

  async createRule(input: CreateRuleInput) {
    return this.prisma.screenTimeRule.create({
      data: {
        familyId: input.familyId,
        deviceId: input.deviceId,
        appPackage: input.appPackage,
        category: input.category,
        dailyLimitMinutes: input.dailyLimitMinutes,
        scheduleStart: input.scheduleStart,
        scheduleEnd: input.scheduleEnd,
      },
    });
  }

  /**
   * El dispositivo del menor sincroniza estas reglas periódicamente y las
   * aplica localmente (ver AppUsageAccessibilityService.kt) para poder
   * bloquear aunque no haya conexión en ese momento. Incluye tanto las
   * reglas específicas de este dispositivo como las de toda la familia
   * (deviceId null).
   */
  async listRulesForDevice(deviceId: string) {
    const device = await this.prisma.device.findUnique({ where: { id: deviceId } });
    if (!device) throw new NotFoundException('Dispositivo no encontrado');

    return this.prisma.screenTimeRule.findMany({
      where: {
        familyId: device.familyId,
        OR: [{ deviceId }, { deviceId: null }],
      },
    });
  }

  async reportLimitReached(familyId: string, deviceId: string, appPackage: string) {
    await this.alerts.create({
      familyId,
      deviceId,
      type: 'SCREEN_TIME_LIMIT_REACHED',
      message: `Límite alcanzado para ${appPackage}`,
    });
  }
}
