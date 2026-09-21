import { Controller, Get, Param } from '@nestjs/common';
import { FamilyService } from './family.service';

@Controller('families')
export class FamilyController {
  constructor(private readonly family: FamilyService) {}

  @Get(':familyId/summary')
  getSummary(@Param('familyId') familyId: string) {
    return this.family.getFamilySummary(familyId);
  }
}
