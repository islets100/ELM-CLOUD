import os, re

body_dir = os.path.join(os.path.dirname(__file__), 'body')

for fname in sorted(os.listdir(body_dir)):
    if not fname.endswith('.tex'):
        continue
    fpath = os.path.join(body_dir, fname)
    with open(fpath, 'rb') as f:
        data = f.read()

    original = data

    # Fix float specifiers: [htbp] -> [H]
    data = data.replace(b'\\begin{figure}[htbp]', b'\\begin{figure}[H]')
    data = data.replace(b'\\begin{table}[htbp]', b'\\begin{table}[H]')
    data = data.replace(b'\\begin{table}[h]', b'\\begin{table}[H]')

    if data != original:
        with open(fpath, 'wb') as f:
            f.write(data)
        n = (original.count(b'\\begin{figure}[htbp]') +
             original.count(b'\\begin{table}[htbp]') +
             original.count(b'\\begin{table}[h]'))
        print(f'{fname}: {n} float fixes')
    else:
        print(f'{fname}: no changes')
