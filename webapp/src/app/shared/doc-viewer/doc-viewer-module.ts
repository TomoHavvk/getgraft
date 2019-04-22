import {DocViewer} from './doc-viewer';
import {
  MatButtonModule,
  MatIconModule,
  MatTabsModule,
  MatTooltipModule,
  MatSnackBarModule,
} from '@angular/material';
import {PortalModule} from '@angular/cdk/portal';
import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {HeaderLink} from './header-link';
import {CopierService} from '../copier/copier.service';


@NgModule({
  imports: [
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatTabsModule,
    CommonModule,
    PortalModule
  ],
  providers: [CopierService],
  declarations: [DocViewer, HeaderLink],
  entryComponents: [ HeaderLink],
  exports: [DocViewer, HeaderLink],
})
export class DocViewerModule { }
