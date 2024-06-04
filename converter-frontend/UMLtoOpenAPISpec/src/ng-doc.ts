import { NgDocConfiguration } from '@ng-doc/builder';

const config: NgDocConfiguration = {
  cache: true,
  pages: 'src/docs',
  outDir: 'src/assets/ng-docs',
  routePrefix: 'docs',
  tsConfig: 'tsconfig.app.json',
  keywords: {
    loaders: [],
    keywords: {}
  }
};

export default config;
