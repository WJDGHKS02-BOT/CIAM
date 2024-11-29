import '../../../_components/spinner/loading-spinner.js';

import Observable from "../@core/observer.js";
import {PROCESS_STATUS} from "./process-manager.constants.js";

class ProcessManager extends Observable {
  constructor() {
    super();
    this._state = this.initial();
  }

  get status() {
    return this._status;
  }

  initial() {
    this._status = PROCESS_STATUS.initial;
    this.notify(this._status);
    return this._status;
  }

  loading() {
    this._status = PROCESS_STATUS.loading;
    this.notify(this._status);
    return this._status;
  }

  success() {
    this._status = PROCESS_STATUS.success;
    this.notify(this._status);
    return this._status;
  }

  error(error) {
    this._status = PROCESS_STATUS.error(error);
    this.notify(this._status);
    return this._status;
  }
}

const processManager = new ProcessManager();

processManager.subscribe((status) => {
  switch (status) {
    case PROCESS_STATUS.loading:
      return showLoadingSpinner();
    case PROCESS_STATUS.initial:
    case PROCESS_STATUS.success:
      return hideLoadingSpinner();
    case PROCESS_STATUS.error:
      hideLoadingSpinner();
      throw new Error(`[ERROR]: ${status.error}`);
    default:
      console.error('Invalid status:', status);
      break;
  }
});

window.processManager = processManager;

export default processManager;