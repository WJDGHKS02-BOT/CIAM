import { userValidator } from './user-validator';
import { gigyaAPI } from '@/features/gigya';
import { authRedirect, consentVersionCheck, setAccountStatus } from '@/features';

import type { ChannelType, GigyaAccountInfo } from '@/types';

type UserStatus = 'active' | 'inactive' | 'regSubmit' | 'regEmailVerified' | 'disabled';
type ApprovalStatus = 'approved' | 'inactive' | 'pending' | 'emp-pending' | 'disabled';

interface ValidationResult {
  success: boolean;
  error?: string;
  redirect?: () => void;
}

const { hasChannel, isAdUser, isPasswordChangeRequired, isSFDCChannel } = userValidator;

export class AuthValidator {
  constructor(
    private user: GigyaAccountInfo,
    private channel: ChannelType,
  ) {}

  async validatePassword(): Promise<ValidationResult> {
    try {
      if (!isAdUser(this.user) && isPasswordChangeRequired(this.user)) {
        const res = await gigyaAPI.accounts.resetPassword({
          loginID: this.user.profile.email,
        });

        if (res) {
          return {
            success: false,
            redirect: () => authRedirect.approvalStatusError('pending'),
          };
        }

        return {
          success: false,
          error: '비밀번호 재설정 이메일 발송에 실패했습니다. 다시 시도해주세요.',
        };
      }

      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : '알 수 없는 에러가 발생했습니다.',
      };
    }
  }

  async validateStatus(): Promise<ValidationResult> {
    try {
      if (isAdUser(this.user) && !hasChannel(this.user)) {
        const redirect =
          this.channel === 'btp'
            ? () => authRedirect.approvalStatusError('btp')
            : () => authRedirect.javaNewChannelAD(import.meta.env.VITE_HOST_URL_JAVA, this.user.UID, this.channel);

        return { success: false, redirect };
      }

      await this.validateUserStatus();
      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : '알 수 없는 에러가 발생했습니다.',
      };
    }
  }

  async validateConsent(): Promise<ValidationResult> {
    try {
      const isRequiredUpdateConsent = (await consentVersionCheck(this.user.UID, this.channel)) === 'Y';

      if (isRequiredUpdateConsent) {
        return {
          success: false,
          redirect: () => authRedirect.consentUpdate(this.user.UID, this.channel),
        };
      }

      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : '알 수 없는 에러가 발생했습니다.',
      };
    }
  }

  async recordLastLogin(channel: ChannelType): Promise<ValidationResult> {
    try {
      if (this.channel === 'btp') {
        return { success: true };
      }

      await gigyaAPI.accounts.setAccountInfo({ lastLogin: new Date(), channel });
      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : '마지막 로그인 시간 업데이트에 실패했습니다.',
      };
    }
  }

  async validateUserStatus(): Promise<ValidationResult> {
    try {
      const status = this.user.data.userStatus as UserStatus;

      switch (status) {
        case 'active':
        case 'inactive':
          if (this.channel === 'btp') {
            return { success: true };
          }
          return this.validateApprovalStatus();

        case 'regSubmit':
          return {
            success: false,
            redirect: () => authRedirect.approvalStatusError('regSubmit'),
          };

        case 'regEmailVerified':
          return {
            success: false,
            redirect: () => authRedirect.approvalStatusError('regEmailVerified'),
          };

        default:
          return {
            success: false,
            redirect: () => authRedirect.approvalStatusError('disabled'),
          };
      }
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : '사용자 상태 확인 중 에러가 발생했습니다.',
      };
    }
  }

  private async validateApprovalStatus(): Promise<ValidationResult> {
    try {
      const status = this.user.data.approvalStatus as ApprovalStatus;

      switch (status) {
        case 'approved':
        case 'inactive':
          await setAccountStatus(this.user.UID, this.channel);
          return { success: true };

        case 'pending':
        case 'emp-pending':
        case 'disabled':
          return {
            success: false,
            redirect: () => authRedirect.approvalStatusError(status),
          };

        default:
          if (isAdUser(this.user)) {
            return {
              success: false,
              redirect: () =>
                authRedirect.javaNewChannelAD(import.meta.env.VITE_HOST_URL_JAVA, this.user.UID, this.channel),
            };
          }

          if (isSFDCChannel(this.channel)) {
            return {
              success: false,
              redirect: () => authRedirect.phpMyPage(import.meta.env.VITE_HOST_URL_PHP),
            };
          }

          return {
            success: false,
            redirect: () =>
              authRedirect.javaChannelExpand(import.meta.env.VITE_HOST_URL_JAVA, this.user.UID, this.channel),
          };
      }
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : '승인 상태 확인 중 에러가 발생했습니다.',
      };
    }
  }

  async validateAll(): Promise<ValidationResult> {
    try {
      // 비밀번호 검증
      const passwordResult = await this.validatePassword();
      if (!passwordResult.success) return passwordResult;

      // 상태 검증
      const statusResult = await this.validateStatus();
      if (!statusResult.success) return statusResult;

      // 동의 검증
      const consentResult = await this.validateConsent();
      if (!consentResult.success) return consentResult;

      // 마지막 로그인 기록
      const loginResult = await this.recordLastLogin(this.channel);
      if (!loginResult.success) return loginResult;

      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : '인증 프로세스 중 에러가 발생했습니다.',
      };
    }
  }
}
