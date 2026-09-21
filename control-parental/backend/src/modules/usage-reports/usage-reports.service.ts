import { Injectable } from '@nestjs/common';
import { AppCategory } from '@prisma/client';
import { PrismaService } from '../../common/prisma/prisma.service';

interface UsageSessionInput {
  deviceId: string;
  appPackage: string;
  category: AppCategory;
  startedAt: Date;
  endedAt: Date;
}

const ALL_CATEGORIES: AppCategory[] = [
  'EDUCATION',
  'GAMES',
  'SOCIAL',
  'ENTERTAINMENT',
  'PRODUCTIVITY',
  'OTHER',
];

@Injectable()
export class UsageReportsService {
  constructor(private readonly prisma: PrismaService) {}

  async recordSession(input: UsageSessionInput) {
    const durationSeconds = Math.round(
      (input.endedAt.getTime() - input.startedAt.getTime()) / 1000,
    );
    return this.prisma.appUsageSession.create({
      data: { ...input, durationSeconds },
    });
  }

  async getWeeklyReport(deviceId: string) {
    const weekAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);

    const grouped = await this.prisma.appUsageSession.groupBy({
      by: ['category'],
      where: { deviceId, startedAt: { gte: weekAgo } },
      _sum: { durationSeconds: true },
    });

    const minutesByCategory = new Map(
      grouped.map((g) => [g.category, Math.round((g._sum.durationSeconds ?? 0) / 60)]),
    );

    return {
      deviceId,
      range: 'week',
      byCategory: ALL_CATEGORIES.map((category) => ({
        category,
        minutes: minutesByCategory.get(category) ?? 0,
      })),
    };
  }
}
