import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AbstractionEditorComponent } from './abstraction-editor.component';

describe('AbstractionEditorComponent', () => {
  let component: AbstractionEditorComponent;
  let fixture: ComponentFixture<AbstractionEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AbstractionEditorComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AbstractionEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
