import threading
import time
import argparse
import atexit
from currency_tracker import CurrencyTracker

global tracker

def exit_handler():
  tracker.unsubscribe()

if __name__ == "__main__":
  parser = argparse.ArgumentParser(description='Bank server application')
  parser.add_argument('bank_name', type=str)
  parser.add_argument('currencies', nargs='+')
  args = parser.parse_args()

  tracker = CurrencyTracker(args.bank_name, args.currencies)
  atexit.register(exit_handler)

  tracker.subscribe()