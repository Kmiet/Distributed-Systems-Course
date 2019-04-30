const db = require('../db')

function subscribe(call) {
  db.addSubscriber(call.request.bankName, call.request.currencies, call)
  call.request.currencies.forEach(currency => {
    call.write({
      currency,
      exchange_ratio: db.getRatio(currency)
    })
  })
}

function unsubscribe(call) {
  const stream = db.removeSubscriber(call.request.bankName)
  stream.end()
}

module.exports = {
  subscribe,
  unsubscribe
}