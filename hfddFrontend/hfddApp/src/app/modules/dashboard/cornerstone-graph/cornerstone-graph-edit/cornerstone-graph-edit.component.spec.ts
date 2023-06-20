import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CornerstoneGraphEditComponent } from './cornerstone-graph-edit.component';

describe('CornerstoneGraphEditComponent', () => {
  let component: CornerstoneGraphEditComponent;
  let fixture: ComponentFixture<CornerstoneGraphEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CornerstoneGraphEditComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CornerstoneGraphEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
