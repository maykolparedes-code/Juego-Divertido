import { Injectable } from '@nestjs/common';
import { AlertsService } from '../alerts/alerts.service';

interface CreateRuleInput {
  familyId: string;
  deviceId?: string;
  appPackage?: string;
  category?: string;
  dailyLimitMinutes: number;
  scheduleStart?: string;
  scheduleEnd?: string;
}

@Injectable()
export class ScreenTimeService {
  constructor(private readonly alerts: AlertsService) {}

  async createRule(input: CreateRuleInput) {
    // Persistencia real: prisma.screenTimeRule.create({ data: input })
    return { ...input, id: 'generated-id' };
  }

  async listRulesForDevice(deviceId: string) {
    // El dispositivo del menor sincroniza estas reglas periódicamente y
    // las aplica localmente (ver AppUsageAccessibilityService.kt) para
    // poder bloquear aunque no haya conexión en ese momento.
    return [] as unknown[];
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
