const initialData = {
  AUD: 1.0,
  CHF: 2.0,
  EUR: 3.0,
  GBP: 4.0,
  USD: 5.0
}

const db = {
  currencies: {

  },
  currency_subscribers: {
    AUD: [],
    CHF: [],
    EUR: [],
    GBP: [],
    USD: []
  },
  subscribers: {}
}

function init() {
  Object.entries(initialData).forEach(currencyData => {
    let [key, val] = currencyData
    db["currencies"][key] = val
    db["currency_subscribers"][key] = []
    setTimeout(() => _updateCurrency(key), 10000)
  })
}

function addSubscriber(name, currencies, stream) {
  db["subscribers"][name] = currencies
  currencies.forEach(currency => {
    db["currency_subscribers"][currency][name] = stream
  })
}

function getRatio(key) {
  return db["currencies"][key]
}

function removeSubscriber(name) {
  let stream;
  currencies = db["subscribers"][name]
  currencies.forEach(currency => {
    stream = db["currency_subscribers"][currency][name]
    delete db["currency_subscribers"][currency][name]
  })
  delete db["subscribers"][name]
  return stream
}

function _randMult() {
  let diff = Math.random() / 16
  if(Math.random() < 0.5) return 1 + diff
  else return 1 - diff
}

function _randTime() {
  return 5000 + Math.floor(5000 * Math.random())
}

function _updateCurrency(key) {
  let new_ratio = db["currencies"][key] * _randMult()
  db["currencies"][key] = new_ratio
  console.log(key, new_ratio)
  Object.values(db["currency_subscribers"][key]).forEach(stream => {
    stream.write({
      currency: key,
      exchange_ratio: new_ratio
    })
  }) 

  setTimeout(() => _updateCurrency(key), _randTime())
}

module.exports = {
  init,
  addSubscriber,
  getRatio,
  removeSubscriber
}