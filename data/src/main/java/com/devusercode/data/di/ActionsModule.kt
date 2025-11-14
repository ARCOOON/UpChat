@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.devusercode.data.di

import com.devusercode.core.domain.auth.actions.*
import com.devusercode.core.domain.auth.repo.AuthRepository
import com.devusercode.core.domain.chat.actions.ListOpenConversations
import com.devusercode.core.domain.chat.repo.ChatRepository
import com.devusercode.core.domain.user.actions.*
import com.devusercode.core.domain.user.repo.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ActionsModule {
    // user/chat actions
    @Provides fun provideGetCurrentUser(repo: UserRepository) = GetCurrentUser(repo)

    @Provides fun provideSetOnlineStatus(repo: UserRepository) = SetOnlineStatus(repo)

    @Provides fun provideObservePresence(repo: UserRepository) = ObservePresence(repo)

    @Provides fun provideSignOut(repo: UserRepository) = SignOut(repo)

    @Provides fun provideListOpenConversations(repo: ChatRepository) = ListOpenConversations(repo)

    // NEW auth actions
    @Provides fun provideSignInWithEmail(repo: AuthRepository) = SignInWithEmail(repo)

    @Provides fun provideSendPasswordReset(repo: AuthRepository) = SendPasswordReset(repo)

    @Provides fun provideObserveRememberMe(repo: AuthRepository) = ObserveRememberMe(repo)

    @Provides fun provideObserveSavedEmail(repo: AuthRepository) = ObserveSavedEmail(repo)

    @Provides fun provideSetRememberMe(repo: AuthRepository) = SetRememberMe(repo)

    @Provides fun provideSetSavedEmail(repo: AuthRepository) = SetSavedEmail(repo)

    @Provides fun provideSignUpWithEmail(repo: AuthRepository) = SignUpWithEmail(repo)

    @Provides fun provideDeleteCurrentUser(repo: AuthRepository) = DeleteCurrentUser(repo)

    @Provides fun provideReauthenticate(repo: AuthRepository) = Reauthenticate(repo)
}
