# LivesWithTornado
竜巻を発生させることができるプラグイン
動作環境: minecraft: 1.16.5

## コマンド

* tornado  
  * summon  
     * entity  
       * \<selector>  
         * \<tornadoName>  
           * \<radius>
             * \<height>
               * \<speed>
      * location
        * \<world>
          * \<x>
            * \<y>
              * \<z>
                * \<tornadoName>
                  * \<radius>
                    * \<height>
                      * \<speed>
  * remove
    * \<tornadoName>
  * modify
    * \<tornadoName>
      * \<settingItem>
        * \<value>

## 設定項目(settingItem)の説明
* radius 
竜巻の半径の設定
* height
竜巻の高さの設定
* speed
竜巻に巻き込まれているエンティティの1ティック辺りの回転角
* riseCoef
竜巻に巻き込まれているエンティティの1ティック辺りの上昇係数
* centrifugalCoef
竜巻に巻き込まれているエンティティの1ティック辺りの遠心力係数
* exceptCreatives
クリエイティブモードのプレイヤーを巻き込むかどうか
* exceptSpectators
スペクテイターモードのプレイヤーを巻き込むかどうか
* exceptFlowing
溶岩流と水流を巻き込むかどうか
* exceptSource
溶岩原と水源を巻き込むかどうか
* effectEnabled
竜巻のエフェクトを発生させるかどうか
* limit(limitInvolvedEntity)
巻き込むエンティティの上限値
* involveBlockProbability
ブロックを巻き込む確率値
* involveEntityProbability
エンティティを巻き込む確率値
* followingSpeed
summon locationにより出した竜巻の追跡速度(default: 0.3)

## modifyコマンドに関する補足
settingItemの内boolean値を書き込む項目,具体的にはexceptCreatives,exceptSpectators,exceptFlowing,effectEnabledは\<value>が0の時false,0以外の時にはtrueが書き込まれる.
