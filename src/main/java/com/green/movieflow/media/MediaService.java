package com.green.movieflow.media;

import com.green.movieflow.common.Const;
import com.green.movieflow.common.ResVo;
import com.green.movieflow.media.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaService {
    private final MediaMapper mapper;

    // 메인페이지
    public List<SelMediaVo> getMediaAll(MidiaAllSelDto dto){
        List<SelMediaVo> list = mapper.selMediaAll(dto);
        // 사진을 모두 셀렉 해 와 n+1 처리 하였으며 마지막에 사진 삭제 해 주었습니다.
        // n+1 작업을 하기 위해 사진을 빼와 성능이 나쁩니다.
        List<Integer> imediaList = new ArrayList<>();
        HashMap<Integer, SelMediaVo> mediaMap = new HashMap<>();
        for ( SelMediaVo vo : list ) {
            imediaList.add(vo.getImedia());
            mediaMap.put(vo.getImedia(), vo);
        }
        List<SelMediaPicsProcVo> pics = mapper.selMediapic(imediaList);
        for ( SelMediaPicsProcVo pic : pics ) {
            mediaMap.get(pic.getImedia()).getPics().add(pic.getPic());
        }
        for ( SelMediaVo vo : list ) {
            while (vo.getPics().size() > 1){
                vo.getPics().remove(vo.getPics().size() - 1);
            }
            vo.setPic(vo.getPics().get(0));
        }
        return list;
    }

    // 날짜별 media리스트
    public List<MediaDaySelVo> getDayMedia(MediaDaySelDto dto){
        List<MediaDaySelVo> list = mapper.dayMedia(dto);
        log.info("list : {}", list);
        return list;
    }

    // 마이페이지
    public List<SelMediaAllVo> getMedia(SelMediaAllDto dto){
        List<SelMediaAllProcVo> list = mapper.selMedia(dto);
        // sawInfo 안에는 isSaw가 1일 때 사용되는 값을 저장하도록 했습니다.
        List<SelMediaAllVo> allVo = new ArrayList<>();
        for ( SelMediaAllProcVo pVo: list ) {
            SelMediaAllVo vo = SelMediaAllVo.builder()
                    .imedia(pVo.getImedia())
                    .title(pVo.getTitle())
                    .date(pVo.getDate())
                    .pic(pVo.getPic())
                    .build();
            if(pVo.getStar() != null){
                // 스웨거 꾸미기 작업 입니다. 성능이 살짝 나빠집니다.
                // star, comment 가 있다면 객체 생성
                MediaSawInfoVo sawInfoVo = new MediaSawInfoVo();
                sawInfoVo.setStar(pVo.getStar());
                sawInfoVo.setComment(pVo.getComment());
                vo.setSawInfo(sawInfoVo);
            }
            allVo.add(vo);
        }
        return allVo;
    }

    // 상세페이지
    public SelMediaDetailVo getDetailMedia(SelMediaDto dto){
        SelMediaDetailVo vo = mapper.selDetailMedia(dto);
        List<String> pics = mapper.selMediaPics(dto);
        vo.setPics(pics);
        return vo;
    }

    // 미디어 등록
    public ResVo postMedia(InsMediaDto dto){
        mapper.insMedia(dto);
        return new ResVo(dto.getImedia());
    }

    // 시청여부 수정
    public ResVo patchIsSaw(MediaPatIsSawDto dto){
        return new ResVo(mapper.patchIsSaw(dto));
    }

    // 미디어 수정
    public ResVo putMedia(PutMedia dto){
        // 사진 제외 수정
        mapper.putMedia(dto);
        // 사진 따로 수정, 모두 삭제 후 등록
        if (dto.getPics().size() >0){
            DelMediaDto dto1 = new DelMediaDto();
            dto1.setImedia(dto.getImedia());
            mapper.delMediaPics(dto1);
            InsMediaDto dto2 = new InsMediaDto();
            dto2.setImedia(dto.getImedia());
            dto2.setPics(dto.getPics());
            mapper.insMediaPics(dto2);
        }
        return new ResVo(Const.SUCCESS);
    }

    // media 삭제
    public ResVo delMedia(DelMediaDto dto){
        // select로 있는지 확인 후 삭제
        Integer imedia = mapper.selMediaByDelMedia(dto);
        if(imedia == null){
            return new ResVo(Const.FAIL);
        }
        mapper.delMedia(dto);
        return new ResVo(Const.SUCCESS);
    }
}
