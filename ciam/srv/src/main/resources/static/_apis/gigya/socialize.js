import socialize_notifyLogin from "./socialize/socialize.notifyLogin.js";
import socialize_logout from "./socialize/socialize.logout.js";

const socialize = {
  logout: socialize_logout,
  notifyLogin: socialize_notifyLogin,
}

window.socialize = socialize;