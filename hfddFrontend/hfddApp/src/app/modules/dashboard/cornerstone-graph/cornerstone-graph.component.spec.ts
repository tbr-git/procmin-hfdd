import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CornerstoneGraphComponent } from './cornerstone-graph.component';

describe('CornerstoneGraphComponent', () => {
  let component: CornerstoneGraphComponent;
  let fixture: ComponentFixture<CornerstoneGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CornerstoneGraphComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CornerstoneGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
