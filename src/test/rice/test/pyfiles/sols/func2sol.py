def func2(dict_val):
    retval = []
    for key, val in dict_val.items():
        retval.append(key + str(val))

    retval.sort()
    return retval

import sys
if __name__ == "__main__":
  args = sys.argv[1:]
  args = [eval(arg) for arg in args]
  result = func2(*args)
  print(result)
