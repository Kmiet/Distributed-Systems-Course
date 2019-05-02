from threading import Thread
import grpc
import currency_tracker_pb2
import currency_tracker_pb2_grpc as pb2_grpc

HOST = '127.0.0.1'
PORT = '50051'

class CurrencyTracker():
  def __init__(self, name, currencies):
    self.channel = grpc.insecure_channel(HOST + ':' + PORT)
    self.stub = pb2_grpc.CurrencyTrackerStub(self.channel)
    self.name = name
    self.currencies = currencies
    self.current_ratio = dict()
    self.current_ratio['PLN'] = 1

  def get_currencies(self):
    return self.currencies

  def get_exchange_ratio(self, currency):
    print(self.current_ratio)
    return self.current_ratio[currency]

  def subscribe(self):
    sub_thread = Thread(target=self._receive_uptades)
    sub_thread.start()
    sub_thread.join()

  def unsubscribe(self):
    cancelation = currency_tracker_pb2.Cancelation(bankName=self.name)
    self.stub.unsubscribe(cancelation)

  def _get_currency_string(self, enum):
    if enum == 0:
      return 'AUD'
    elif enum == 1:
      return 'CHF'
    elif enum == 2:
      return 'EUR'
    elif enum == 3:
      return 'GBP'
    elif enum == 4:
      return 'USD'
    else:
      return enum

  def _receive_uptades(self):
    subscription = currency_tracker_pb2.Subscription(
      bankName=self.name,
      currencies=self.currencies
    )
    for currency_data in self.stub.subscribe(subscription):
      curr = self. _get_currency_string(currency_data.currency)
      self.current_ratio[curr] = currency_data.exchange_ratio