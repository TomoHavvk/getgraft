import {Injectable} from '@angular/core';

export interface GuideItem {
  id: string;
  name: string;
  url: string;
}

const GUIDES = [
  {
    id: 'getting-started',
    name: 'Getting started',
    url: 'https://github.com/graft-community/docs/blob/master/Graft_Supernode_Mainnet_Simple-step-by-step-setup-instructions-for-non-Linux-users_v1.7.md',
  },
  {

    id: 'windows-compilation',
    name: 'Windows Compile Instructions For GRAFT NETWORK',
    url: 'https://github.com/graft-community/docs/blob/master/Graft%20Network%20Windows%20Compile.md'
  }
  ,

  {
    id: 'theming',
    name: 'Theming Angular Material',
    url: '/guides/theming.html',
  },
  // {
  //   id: 'theming-your-components',
  //   name: 'Theming your own components',
  //   url: '/docs-content/guides/theming-your-components.html',
  // },
  // {
  //   id: 'typography',
  //   name: `Using Angular Material's Typography`,
  //   url: '/docs-content/guides/typography.html',
  // },
  // {
  //   id: 'customizing-component-styles',
  //   name: 'Customizing component styles',
  //   url: '/docs-content/guides/customizing-component-styles.html'
  // },
  // {
  //   id: 'creating-a-custom-form-field-control',
  //   name: 'Creating a custom form field control',
  //   url: '/docs-content/guides/creating-a-custom-form-field-control.html'
  // },
  // {
  //   id: 'elevation',
  //   name: 'Using elevation helpers',
  //   url: '/docs-content/guides/elevation.html'
  // },
  // {
  //   id: 'creating-a-custom-stepper-using-the-cdk-stepper',
  //   name: 'Creating a custom stepper using the CdkStepper',
  //   url: '/docs-content/guides/creating-a-custom-stepper-using-the-cdk-stepper.html'
  // }
];

@Injectable()
export class GuideItems {

  getAllItems(): GuideItem[] {
    return GUIDES;
  }

  getItemById(id: string): GuideItem {
    return GUIDES.find(i => i.id === id);
  }
}
