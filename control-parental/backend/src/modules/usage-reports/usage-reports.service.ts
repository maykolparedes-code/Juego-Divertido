import { Injectable } from '@nestjs/common';

interface UsageSessionInput {
  deviceId: string;
  appPackage: string;
  category: string;
  startedAt: Date;
  endedAt: Date;
}

@Injectable()
export class UsageReportsService {
  async recordSession(input: UsageSessionInput) {
    const durationSeconds = Math.round(
      (input.endedAt.getTime() - input.startedAt.getTime()) / 1000,
    );
    // Persistencia real: prisma.appUsageSession.create({ data: { ...input, durationSeconds } })
    return { ...input, durationSeconds };
  }

  async getWeeklyReport(deviceId: string) {
    // Implementación real: agregación SQL agrupando por categoría y día
    // (prisma.appUsageSession.groupBy) sobre los últimos 7 días.
    return {
      deviceId,
      range: 'week',
      byCategory: [
        { category: 'EDUCATION', minutes: 0 },
        { category: 'GAMES', minutes: 0 },
        { category: 'SOCIAL', minutes: 0 },
        { category: 'ENTERTAINMENT', minutes: 0 },
      ],
    };
  }
}
