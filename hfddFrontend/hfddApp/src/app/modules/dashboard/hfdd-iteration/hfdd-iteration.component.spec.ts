import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HfddIterationComponent } from './hfdd-iteration.component';

describe('HfddIterationComponent', () => {
  let component: HfddIterationComponent;
  let fixture: ComponentFixture<HfddIterationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HfddIterationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HfddIterationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
