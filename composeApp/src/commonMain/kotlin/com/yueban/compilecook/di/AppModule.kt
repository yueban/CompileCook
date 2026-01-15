package com.yueban.compilecook.di

import com.yueban.compilecook.service.DefaultMessageService
import com.yueban.compilecook.service.MessageService
import org.koin.dsl.module

val appModule = module {
  single<MessageService> { DefaultMessageService() }
}
