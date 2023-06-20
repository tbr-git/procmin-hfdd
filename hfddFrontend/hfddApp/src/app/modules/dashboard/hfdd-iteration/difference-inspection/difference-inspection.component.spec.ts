import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DifferenceInspectionComponent } from './difference-inspection.component';

describe('DifferenceInspectionComponent', () => {
  let component: DifferenceInspectionComponent;
  let fixture: ComponentFixture<DifferenceInspectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DifferenceInspectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DifferenceInspectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
