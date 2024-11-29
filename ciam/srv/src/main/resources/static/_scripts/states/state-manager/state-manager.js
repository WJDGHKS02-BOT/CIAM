import Observable from "../@core/observer.js";

export class StateManager extends Observable {
  constructor(initialState = {}) {
    super();
    this._state = initialState;
  }

  get state() {
    return {...this._state};
  }

  set state(newState) {
    this._state = {...this._state, ...newState};
    this.notify(this._state);
  }
}