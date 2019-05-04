import threading
import time
import argparse
import atexit
from currency_tracker import CurrencyTracker
from bank_manager import BankManager
from user_manager import UserManager
from account_manager import AccountManager

global tracker

def exit_handler():
  tracker.unsubscribe()

if __name__ == "__main__":
  parser = argparse.ArgumentParser(description='Bank server application')
  parser.add_argument('bank_name', type=str)
  parser.add_argument('service_port')
  parser.add_argument('currencies', nargs='+')
  args = parser.parse_args()

  tracker = CurrencyTracker(args.bank_name, args.currencies)

  user_manager = UserManager()
  bank_manager = BankManager(user_manager, tracker, args.bank_name, args.service_port)
  bank_manager.start()