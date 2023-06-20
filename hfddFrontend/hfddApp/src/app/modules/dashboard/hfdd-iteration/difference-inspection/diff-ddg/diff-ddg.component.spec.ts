import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DiffDdgComponent } from './diff-ddg.component';

describe('DiffDdgComponent', () => {
  let component: DiffDdgComponent;
  let fixture: ComponentFixture<DiffDdgComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DiffDdgComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DiffDdgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
