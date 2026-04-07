package com.yueban.compilecook.ui.util.preview

import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.repo.entity.DishDetail
import com.yueban.compilecook.repo.entity.DishSummary
import com.yueban.compilecook.repo.entity.TipDetail
import com.yueban.compilecook.repo.entity.TipSummary
import com.yueban.compilecook.repo.entity.TipType

object PreviewConstant {
  const val ABOUT_LIBRARIES = """
    {
        "libraries": [
            {
                "uniqueId": "androidx.activity:activity",
                "artifactVersion": "1.12.3",
                "name": "Activity",
                "description": "Provides the base Activity subclass and the relevant hooks to build a composable structure on top.",
                "website": "https://developer.android.com/jetpack/androidx/releases/activity#1.12.3",
                "developers": [
                    {
                        "name": "The Android Open Source Project"
                    }
                ],
                "organization": {
                    "name": "The Android Open Source Project"
                },
                "scm": {
                    "connection": "scm:git:https://android.googlesource.com/platform/frameworks/support",
                    "url": "https://cs.android.com/androidx/platform/frameworks/support"
                },
                "licenses": [
                    "Apache-2.0"
                ],
                "funding": []
            },
            {
                "uniqueId": "androidx.activity:activity-compose",
                "artifactVersion": "1.12.3",
                "name": "Activity Compose",
                "description": "Compose integration with Activity",
                "website": "https://developer.android.com/jetpack/androidx/releases/activity#1.12.3",
                "developers": [
                    {
                        "name": "The Android Open Source Project"
                    }
                ],
                "organization": {
                    "name": "The Android Open Source Project"
                },
                "scm": {
                    "connection": "scm:git:https://android.googlesource.com/platform/frameworks/support",
                    "url": "https://cs.android.com/androidx/platform/frameworks/support"
                },
                "licenses": [
                    "Apache-2.0"
                ],
                "funding": []
            }
        ],
        "licenses": {
            "Apache-2.0": {
                "name": "Apache License 2.0",
                "url": "https://spdx.org/licenses/Apache-2.0.html",
                "content": "",
                "internalHash": "Apache-2.0",
                "spdxId": "Apache-2.0",
                "hash": "Apache-2.0"
            }
        }
    }
  """

  val dishSummary = DishSummary(
    name = "炒意大利面",
    pinyin = "chaoyidalimian",
    description = "这是一道软糯爽口的意大利面的做法，非常简单，用时大概 30 分钟。",
    category = DishCategory.STAPLE,
    difficulty = 3,
    image = "https://media.githubusercontent.com/media/Anduin2017/HowToCook/master/dishes/staple/炒意大利面/a.jpg",
    isFavorite = false,
  )

  val dishDetail = DishDetail(
    name = dishSummary.name,
    pinyin = dishSummary.pinyin,
    description = dishSummary.description,
    category = dishSummary.category,
    difficulty = dishSummary.difficulty,
    image = dishSummary.image,
    isFavorite = dishSummary.isFavorite,
    content = """
      # 炒意大利面的做法
    
      ![意大利面](https://media.githubusercontent.com/media/Anduin2017/HowToCook/master/dishes/staple/炒意大利面/a.jpg)
      
      这是一道软糯爽口的意大利面的做法，非常简单，用时大概 30 分钟。
      
      预估烹饪难度：★★★
      
      ## 必备原料和工具
      
      - 意大利面
      - 肥牛片
      - 番茄酱 / 黑胡椒酱（选其一即可）
      - 菜籽油（其他植物油也可）
      
      ## 计算
      
      - 意大利面 50 克 / 人
      - 肥牛 5 片 / 人
      - 食用油 5ml / 50 克意面
      
      ## 操作
      
      - 加入 250 克水 / 人
      - 待水烧开，下入面条，中火煮 15 - 20 分钟（这个面通常比较硬，捞起来之前最好尝一下，中心如果有一点硬，需要继续煮）
      - 捞出面条，盛入盘中备用
      - 热锅倒入食用油，待油温中热，下入面条翻炒一分钟（如果太干，加入少量水）
      - 放入 10 克番茄酱、肥牛、加入 2g 食盐，继续翻炒一分钟
      - 起锅
    """.trimIndent(),
  )

  val tipSummary = TipSummary(
    name = "如何选择现在吃什么",
    pinyin = "ruhexuanzexianzaichishenme",
    type = TipType.BASIC,
    isFavorite = false
  )

  val tipDetail = TipDetail(
    name = tipSummary.name,
    pinyin = tipSummary.pinyin,
    type = tipSummary.type,
    isFavorite = tipSummary.isFavorite,
    content = """
        # 如何决策吃什么

        如何决策吃什么也是我做菜之前一大难题。所以只能用数学描述一下了。

        ## 计算方法

        ### 计算荤菜和素菜数量

        * 菜的数量 = 人数 + 1。
        * 荤菜比素菜多一个，或一样多即可。

        由此得到荤菜数量和素菜数量，再在上一步的菜谱中选择即可。

        #### 形式语言描述

        当 有人数 `N` 时，
        设 `素菜数` 为 `a`, `荤菜数`为 `b`。
        `N`, `a`, `b`均为整数。

        此时有下列不等式组：

        * a + b = N + 1
        * a ≤ b ≤ a+1

        解得

        ```javascript
        const a = Math.floor((N+1)/2);
        const b = Math.ceil((N+1)/2);
        ```

        ### 菜的选择

        * 如果人数超过 8 人，考虑在荤菜中增加鱼类荤菜。
        * 如果有小孩，考虑增加有甜味的菜。
        * 考虑增加特色菜、拿手菜。
        * 注意决策荤菜时不要全部使用同一种动物的肉。考虑顺序为：`猪肉`、`鸡肉`、`牛肉`、`羊肉`、`鸭肉`、`鱼肉`。
        * 不要选择奇奇怪怪的动物做荤菜。

        如果仍然拿不准，请使用 [今天吃什么?](https://github.com/ryanuo/whatToEat) 工具来选择今天吃什么。
    """.trimIndent()
  )
}
