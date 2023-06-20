import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ItemsetDifferenceViewComponent } from './itemset-difference-view.component';

describe('ItemsetDifferenceViewComponent', () => {
  let component: ItemsetDifferenceViewComponent;
  let fixture: ComponentFixture<ItemsetDifferenceViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ItemsetDifferenceViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ItemsetDifferenceViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
