import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule} from '@angular/core';
import {LocationStrategy, PathLocationStrategy} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {MatNativeDateModule} from '@angular/material';
import {ExampleModule} from '@angular/material-examples';

import {MaterialDocsApp} from './material-docs-app';
import {HomepageModule} from './pages/homepage';
import {MATERIAL_DOCS_ROUTES} from './routes';

import {FooterModule} from './shared/footer/footer';
import {ComponentPageTitle} from './pages/page-title/page-title';
import {StyleManager} from './shared/style-manager';
import {SvgViewerModule} from './shared/svg-viewer/svg-viewer';
import {ThemePickerModule} from './shared/theme-picker';
import {NavBarModule} from './shared/navbar';
import {ThemeStorage} from './shared/theme-picker/theme-storage/theme-storage';
import {GuideItems} from './shared/guide-items/guide-items';
import {DocumentationItems} from './shared/documentation-items/documentation-items';
import {GuideListModule} from './pages/guide-list';
import {WatchListModule} from './pages/watch-list';
import {GettingStartedModule} from './pages/getting-started';
import {SupernodeListModule} from './pages/supernode-list';
import {GuideViewerModule} from './pages/guide-viewer';
import {DocViewerModule} from './shared/doc-viewer/doc-viewer-module';
import {CookieBackendService, CookieModule, CookieService} from 'ngx-cookie';
import {ComponentListModule} from './pages/component-list';
import {ComponentViewerModule} from './pages/component-viewer/component-viewer';
import {ComponentCategoryListModule} from './pages/component-category-list/component-category-list';
import {ComponentSidenavModule} from './pages/component-sidenav/component-sidenav';
import {ComponentHeaderModule} from './pages/component-page-header/component-page-header';
import {StackblitzButtonModule} from './shared/stackblitz';

import {
  CanActivateComponentSidenav
} from './pages/component-sidenav/component-sidenav-can-load-guard';
import {HttpClientModule} from '@angular/common/http';
import {GaService} from './shared/ga/ga';

@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ExampleModule,
    FormsModule,
    HttpClientModule,
    MatNativeDateModule,
    RouterModule.forRoot(MATERIAL_DOCS_ROUTES),
    ComponentCategoryListModule,
    ComponentHeaderModule,
    ComponentListModule,
    ComponentSidenavModule,
    ComponentViewerModule,
    DocViewerModule,
    FooterModule,
    GuideListModule,
    WatchListModule,
    GettingStartedModule,
    SupernodeListModule,
    GuideViewerModule,
    HomepageModule,
    NavBarModule,
    StackblitzButtonModule,
    CookieModule.forRoot(),
    SvgViewerModule,
    ThemePickerModule,
  ],
  declarations: [MaterialDocsApp],
  providers: [
    ComponentPageTitle,
    DocumentationItems,
    GaService,
    CookieService,
    CookieBackendService,
    GuideItems,
    StyleManager,
    ThemeStorage,
    CanActivateComponentSidenav,
    {provide: LocationStrategy, useClass: PathLocationStrategy},
  ],
  bootstrap: [MaterialDocsApp],
})
export class AppModule {}
