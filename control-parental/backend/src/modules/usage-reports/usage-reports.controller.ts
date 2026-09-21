import { Body, Controller, Get, Param, Post } from '@nestjs/common';
import { AppCategory } from '@prisma/client';
import { IsDateString, IsEnum, IsString } from 'class-validator';
import { UsageReportsService } from './usage-reports.service';

class RecordSessionDto {
  @IsString() deviceId!: string;
  @IsString() appPackage!: string;
  @IsEnum(AppCategory) category!: AppCategory;
  @IsDateString() startedAt!: string;
  @IsDateString() endedAt!: string;
}

@Controller('usage-reports')
export class UsageReportsController {
  constructor(private readonly usageReports: UsageReportsService) {}

  @Post('sessions')
  recordSession(@Body() dto: RecordSessionDto) {
    return this.usageReports.recordSession({
      ...dto,
      startedAt: new Date(dto.startedAt),
      endedAt: new Date(dto.endedAt),
    });
  }

  @Get(':deviceId/weekly')
  weekly(@Param('deviceId') deviceId: string) {
    return this.usageReports.getWeeklyReport(deviceId);
  }
}
