import { TestBed } from '@angular/core/testing';

import { HFDDSessionService } from './hfddsession.service';

describe('HFDDSessionService', () => {
  let service: HFDDSessionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HFDDSessionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
