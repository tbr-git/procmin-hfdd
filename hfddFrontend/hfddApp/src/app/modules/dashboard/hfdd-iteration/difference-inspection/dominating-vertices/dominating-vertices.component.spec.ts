import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DominatingVerticesComponent } from './dominating-vertices.component';

describe('DominatingVerticesComponent', () => {
  let component: DominatingVerticesComponent;
  let fixture: ComponentFixture<DominatingVerticesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DominatingVerticesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DominatingVerticesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
