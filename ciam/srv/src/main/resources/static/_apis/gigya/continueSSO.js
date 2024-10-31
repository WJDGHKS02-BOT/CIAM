function continueSSO() {
  return new Promise(() => {
    return gigya.fidm.saml.continueSSO();
  })
}