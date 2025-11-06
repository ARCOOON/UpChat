package com.devusercode.upchat.di

import com.devusercode.core.domain.chat.actions.ListOpenConversations
import com.devusercode.core.domain.chat.repo.ChatRepository
import com.devusercode.core.domain.user.actions.GetCurrentUser
import com.devusercode.core.domain.user.actions.ObservePresence
import com.devusercode.core.domain.user.actions.SetOnlineStatus
import com.devusercode.core.domain.user.actions.SignOut
import com.devusercode.core.domain.user.repo.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ActionsModule {
    @Provides fun provideGetCurrentUser(repo: UserRepository) = GetCurrentUser(repo)
    @Provides fun provideSetOnlineStatus(repo: UserRepository) = SetOnlineStatus(repo)
    @Provides fun provideObservePresence(repo: UserRepository) = ObservePresence(repo)
    @Provides fun provideSignOut(repo: UserRepository) = SignOut(repo)
    @Provides fun provideListOpenConversations(repo: ChatRepository) = ListOpenConversations(repo)
}
