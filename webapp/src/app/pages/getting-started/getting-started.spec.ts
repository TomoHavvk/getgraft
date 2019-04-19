import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {Observable} from 'rxjs';
import {ActivatedRoute} from '@angular/router';
import {GettingStarted, GettingStartedModule} from './getting-started';
import {DocsAppTestingModule} from '../../testing/testing-module';

const guideItemsId = 'getting-started';

const mockActivatedRoute = {
  fragment: Observable.create(observer => {
    observer.complete();
  }),
  params: Observable.create(observer => {
    observer.next({id: guideItemsId});
    observer.complete();
  })
};


describe('GettingStarted', () => {
  let fixture: ComponentFixture<GettingStarted>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [GettingStartedModule, DocsAppTestingModule],
      providers: [
        {provide: ActivatedRoute, useValue: mockActivatedRoute},
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GettingStarted);
  });

});
