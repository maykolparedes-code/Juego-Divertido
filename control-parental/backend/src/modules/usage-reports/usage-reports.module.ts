import { Module } from '@nestjs/common';
import { UsageReportsController } from './usage-reports.controller';
import { UsageReportsService } from './usage-reports.service';

@Module({
  controllers: [UsageReportsController],
  providers: [UsageReportsService],
})
export class UsageReportsModule {}
