const REGEX = {
  email: /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/i,
  samsungEmail: /^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9-]+\.)?samsung\.com$/
}

const isEmail = (email) => {
  return REGEX.email.test(email)
}

const isSamsungEmail = (email) => {
  return REGEX.samsungEmail.test(email)
}

