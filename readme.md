# OuDiaParser
OuDia形式のファイルをJavaオブジェクトとして読み込み、書き込みを行うライブラリです。

## 対応フォーマット
+ OuDia Ver.１.02.05にて出力されるファイル(拡張子 .oud)
+ OuDiaSecond Ver 2.01にて出力されるファイル(拡張子　.oud2)

## 使用方法
### 読み込み
「new DiaFile(File:oud形式ファイル);」
でファイルの読み込みを行いDiaFileオブジェクトを生成します。
### 書き込み
「DiaFile.saveToOuDiaFile(出力ファイルパス)」でoud形式で出力します。

「DiaFile.saveToFile(出力ファイルパス)」でoud2形式で出力します。

### データ読み書き
DiaFile内の各プロパティはpublicアクセスが可能です。
使用者が各自で読み書きコードを作成してください。

## その他
OuDiaSecond Ver 1.xx.xx形式は番線表記や運用表記が異なるため対応していません。
