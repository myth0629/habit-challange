package com.habitchallenge.backend.challenge.config;

import com.habitchallenge.backend.challenge.domain.Challenge;
import com.habitchallenge.backend.challenge.domain.ChallengeCategory;
import com.habitchallenge.backend.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 챌린지 데이터 초기화
 * 애플리케이션 시작 시 기본 챌린지 데이터를 생성합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChallengeDataInitializer {

    private final ChallengeRepository challengeRepository;

    /**
     * 기본 챌린지 데이터 초기화
     */
    @Bean
    public CommandLineRunner initChallengeData() {
        return args -> {
            if (challengeRepository.count() == 0) {
                log.info("챌린지 데이터 초기화 시작");
                
                List<Challenge> challenges = List.of(
                    Challenge.builder()
                        .title("아침에 일어나서 물 한 잔 마시기")
                        .description("아침에 일어나면 물 한 잔을 마셔서 몸에 수분을 공급하세요. 신진대사를 활성화하고 건강에 도움이 됩니다.")
                        .category(ChallengeCategory.HEALTH)
                        .difficulty(1)
                        .isActive(true)
                        .build(),
                    
                    Challenge.builder()
                        .title("10분 스트레칭하기")
                        .description("하루 중 10분만 투자해서 전신 스트레칭을 해보세요. 근육 이완과 혈액순환에 도움이 됩니다.")
                        .category(ChallengeCategory.HEALTH)
                        .difficulty(2)
                        .isActive(true)
                        .build(),
                    
                    Challenge.builder()
                        .title("20분 독서하기")
                        .description("하루 20분 독서로 지식을 쌓고 스트레스를 해소하세요. 취침 전 독서는 수면의 질도 향상시킵니다.")
                        .category(ChallengeCategory.STUDY)
                        .difficulty(2)
                        .isActive(true)
                        .build(),
                    
                    Challenge.builder()
                        .title("5km 걷기")
                        .description("오늘 하루 5km를 걸어보세요. 걷기는 가장 쉽고 효과적인 유산소 운동입니다.")
                        .category(ChallengeCategory.HEALTH)
                        .difficulty(3)
                        .isActive(true)
                        .build(),
                    
                    Challenge.builder()
                        .title("감사일기 쓰기")
                        .description("오늘 하루 감사했던 일 3가지를 기록해보세요. 긍정적인 마인드 형성에 도움이 됩니다.")
                        .category(ChallengeCategory.MINDFULNESS)
                        .difficulty(1)
                        .isActive(true)
                        .build(),
                    
                    Challenge.builder()
                        .title("새로운 요리 도전하기")
                        .description("오늘은 한 번도 해보지 않은 새로운 요리에 도전해보세요. 요리 실력도 늘고 성취감도 느낄 수 있습니다.")
                        .category(ChallengeCategory.HOBBY)
                        .difficulty(3)
                        .isActive(true)
                        .build(),
                    
                    Challenge.builder()
                        .title("디지털 디톡스 2시간")
                        .description("2시간 동안 모든 전자기기(스마트폰, 컴퓨터, TV 등)를 멀리하고 자신에게 집중하는 시간을 가져보세요.")
                        .category(ChallengeCategory.MINDFULNESS)
                        .difficulty(4)
                        .isActive(true)
                        .build(),
                    
                    Challenge.builder()
                        .title("30분 명상하기")
                        .description("30분 동안 명상을 통해 마음을 안정시키고 내면에 집중해보세요. 스트레스 해소와 집중력 향상에 도움이 됩니다.")
                        .category(ChallengeCategory.MINDFULNESS)
                        .difficulty(3)
                        .isActive(true)
                        .build(),
                    
                    Challenge.builder()
                        .title("지인에게 감사 메시지 보내기")
                        .description("오랫동안 연락하지 않았던 지인에게 안부와 감사의 메시지를 보내보세요. 관계 개선에 도움이 됩니다.")
                        .category(ChallengeCategory.SOCIAL)
                        .difficulty(2)
                        .isActive(true)
                        .build(),
                    
                    Challenge.builder()
                        .title("집 대청소하기")
                        .description("오늘 하루 집 안 구석구석을 깨끗이 청소해보세요. 깨끗한 환경은 기분 전환과 생산성 향상에 도움이 됩니다.")
                        .category(ChallengeCategory.PRODUCTIVITY)
                        .difficulty(4)
                        .isActive(true)
                        .build()
                );
                
                challengeRepository.saveAll(challenges);
                log.info("챌린지 데이터 초기화 완료: {} 개의 챌린지 생성됨", challenges.size());
            } else {
                log.info("이미 챌린지 데이터가 존재합니다. 초기화를 건너뜁니다.");
            }
        };
    }
}
