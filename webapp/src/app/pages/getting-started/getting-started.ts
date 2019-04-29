import {Component, NgModule, OnInit} from '@angular/core';
import {ActivatedRoute, RouterModule, Router} from '@angular/router';
import {GuideItem, GuideItems} from '../../shared/guide-items/guide-items';
import {FooterModule} from '../../shared/footer/footer';
import {DocViewerModule} from '../../shared/doc-viewer/doc-viewer-module';
import {TableOfContentsModule} from '../../shared/table-of-contents/table-of-contents.module';
import {ComponentPageTitle} from '../page-title/page-title';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
@Component({
  selector: 'getting-started',
  templateUrl: './getting-started.html',
  styleUrls: ['./getting-started.scss'],
})
export class GettingStarted implements OnInit {
  name: string = "Getting started";
  guide: string = "/guides/getting-started.html";

  public safeURL: SafeResourceUrl;
  constructor(public componentPageTitle: ComponentPageTitle, private _sanitizer: DomSanitizer) {
    this.componentPageTitle.title = "Getting Started";
    this.safeURL = this._sanitizer.bypassSecurityTrustResourceUrl('https://www.youtube.com/embed/AOC0YINVd9Y');
  }
  ngOnInit(): void {

console.log("DSA")
  }
}

@NgModule({
  imports: [DocViewerModule, FooterModule, RouterModule, TableOfContentsModule],
  exports: [GettingStarted],
  declarations: [GettingStarted],
  providers: [GuideItems, ComponentPageTitle],
})
export class GettingStartedModule {}
