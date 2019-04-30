const initialData = {
  AUD: 1,
  CHF: 2,
  EUR: 3,
  GBP: 4,
  USD: 5
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
    insert("currencies", key, val)
    insert("currency_subscribers", key, [])
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
  currencies = db["subscribers"][key]
  currencies.forEach(currency => {
    stream = db["currency_subscribers"][currency][name]
    delete db["currency_subscribers"][currency][name]
  })
  delete db["subscribers"][key]
  return stream
}

function _randMult() {
  let diff = Math.random() / 20
  if(Math.random() > 0.5) return 1 + diff
  else return 1 - diff
}

function _randTime() {
  return 5000 + Math.floor(5000 * Math.random())
}

function _updateCurrency(key) {
  let new_ratio = db["currencies"][key] * _randMult()
  insert("currencies", key, new_ratio)
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