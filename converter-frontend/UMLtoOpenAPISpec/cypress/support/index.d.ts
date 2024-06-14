declare namespace Cypress {
  interface Chainable {
    addClassWithMethodAndAttribute(): Chainable<void>;
    removeClassElements(): Chainable<void>;
    generateOpenAPISpecification(): Chainable<void>;
    renameExampleValues(): Chainable<void>;
    startMockServer(): Chainable<void>;
    saveSessionAndRestartStepper(): Chainable<void>;
    downloadOpenAPISpecification(): Chainable<void>;
  }
}
