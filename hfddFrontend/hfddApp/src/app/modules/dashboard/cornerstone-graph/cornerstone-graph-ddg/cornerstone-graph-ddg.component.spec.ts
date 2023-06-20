import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CornerstoneGraphDdgComponent } from './cornerstone-graph-ddg.component';

describe('CornerstoneGraphDdgComponent', () => {
  let component: CornerstoneGraphDdgComponent;
  let fixture: ComponentFixture<CornerstoneGraphDdgComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CornerstoneGraphDdgComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CornerstoneGraphDdgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
