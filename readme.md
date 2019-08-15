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

## LICENSE
+ Copyright(c) 2019 KameLong
+ contact:kamelong.com

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program(COPYING.txt).  If not, see <http://www.gnu.org/licenses/>.

このプログラムはGNU GPL version 3(もしくはそれ以上のバージョン)ライセンスに従って配布しています。
このプログラムは製作者の許可を取ることなく、誰でも自由に複製・改変・頒布することが許可されています。
ただし、このプログラムを使用した制作物はGNU-GPLライセンスで配布しなければなりません。
