class Observable {
  constructor() {
    this._observers = new Set();
  }

  subscribe(observer) {
    this._observers.add(observer);
    return () => this._observers.delete(observer);
  }

  notify(data) {
    this._observers.forEach(observer => observer(data));
  }
}

export default Observable;