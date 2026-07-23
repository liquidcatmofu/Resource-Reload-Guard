<p align="center">
  <img src="docs/icon.svg" width="160" alt="Resource Reload Guard アイコン">
</p>

# Resource Reload Guard

[English](README.md) · [変更履歴](CHANGELOG.md)

Resource Reload Guardは、Minecraft Java Edition 1.21.1向けのクライアント専用MODです。

大規模なMOD環境では、クライアントリソースの再読み込みに数分以上かかったり、長時間のGC、停止、クラッシュが発生したりする場合があります。このMODはリロード処理そのものを高速化せず、ユーザー操作による不要・不意のリロードを開始前に制御します。

## 対応環境

- Minecraft 1.21.1
- NeoForge 21.1.x
- Fabric Loader（Fabric APIが必要）

NeoForge版とFabric版は別々のJARです。使用するLoaderに対応するJARをクライアントの `mods` ディレクトリへ配置してください。Fabric版ではFabric APIも導入してください。

## 機能

### リソースパック

- 有効化、無効化、並べ替えを検出
- パックIDと順序が同じ場合は適用を省略
- 選択状態を維持したまま編集画面へ戻る
- 変更を破棄し、画面を開く前の状態へ復元
- 今すぐ適用、または次回起動まで延期

### 言語

- 同じ言語を選んだ場合はリロードを省略
- 適用前に現在の言語と選択した言語を表示
- 今すぐ適用、または次回起動まで延期

### ミップマップレベル

- ビデオ設定画面を離れる前に変更を検出
- ビデオ設定へ戻る、ミップマップ変更だけを破棄、次回起動まで延期、または今すぐ適用
- 「今すぐ適用」を選択した場合だけクライアントテクスチャを再読み込み

### 安全対策

- 危険な操作を初期フォーカスにしない
- `Esc` で安全な画面へ戻る
- 設定されたクライアントtick数と、それに相当する単調時間の両方が経過するまで危険なボタンを無効化
- 初期ロード、MOD内部、サーバーリソースパック、エラー復旧、発生元不明のリロードをグローバルにはキャンセルしない

### F3+T

- バニラの即時リロードを開始前に停止
- 明示的に確認した場合のみ実行
- キーリピートによる確認画面・リロードの重複を防止
- リロード進行中の追加要求を拒否

## 設定

初回起動時にコメント付きTOMLが生成されます。

```text
config/resource_reload_guard.toml
```

| キー | 初期値 | 内容 |
| --- | --- | --- |
| `debugKeyPolicy` | `CONFIRM` | F3+Tのポリシー |
| `resourcePackPolicy` | `CONFIRM` | リソースパック変更のポリシー |
| `languagePolicy` | `CONFIRM` | 言語変更のポリシー |
| `mipmapPolicy` | `CONFIRM` | ミップマップレベル変更のポリシー |
| `skipUnchangedResourcePackApply` | `true` | 無変更のパック選択を省略 |
| `allowDeferredResourcePackApply` | `true` | リソースパック変更の延期を許可 |
| `allowDeferredLanguageApply` | `true` | 言語変更の延期を許可 |
| `allowDeferredMipmapApply` | `true` | ミップマップ変更の延期を許可 |
| `blockUserRequestsWhileReloading` | `true` | リロード中の追加操作を拒否 |
| `dangerousButtonDelayTicks` | `20` | 危険なボタンを有効化するまでの待機tick数 |
| `showLoadedModCount` | `true` | ロード済みMOD数を補助情報として表示 |
| `largeEnvironmentWarningThreshold` | `200` | 補助警告を表示するMOD数の基準 |

ポリシーには `ALLOW`、`CONFIRM`、`BLOCK`、`RESTART_ONLY` を指定できます。MOD数は警告表示にだけ使用し、自動ブロックや所要時間予測には使用しません。

## ビルド

JDK 21を用意し、リポジトリのルートで実行します。Minecraft 1.21.1と生成されるMODはJava 21を対象とします。

```bash
./gradlew clean build
```

Windowsでは次のコマンドを使用します。

```powershell
.\gradlew.bat clean build
```

成果物は `neoforge/build/libs` と `fabric/build/libs` に生成されます。

Fabricの `runClient` には、開発時のランタイム依存としてMod Menuと完全版Fabric APIが追加されます。MODメタデータとアイコンの確認用であり、配布JARには同梱されません。

リリース自動化とリポジトリに必要な設定は[リリース手順](docs/releasing.md)に記載しています。

## 既知の制約

- 再読み込み時間の予測やリロード処理自体の高速化は行いません。
- リソースパック、言語、ビデオ設定画面や、同じバニラメソッドを大幅に変更するMODとはMixin競合が起こる可能性があります。
- サーバーリソースパックや発生元を特定できないリロードは制御対象外です。
- Minecraftプロセスの自動再起動は行いません。

## ライセンスと作者

Copyright © 2026 [LiquidCatMofu](https://github.com/liquidcatmofu)

[MIT License](LICENSE) で公開されています。

リポジトリ: [liquidcatmofu/Resource-Reload-Guard](https://github.com/liquidcatmofu/Resource-Reload-Guard)
