/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/* ------------------------------------------------------------------
 * Copyright (C) 1998-2009 PacketVideo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */
/* -*- c++ -*- */
// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

//           O S C L _ M E M _ A U D I T _ I N T E R N A L S

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

/*! \addtogroup osclmemory OSCL Memory
 *
 * @{
 */


/*! \file oscl_mem_audit_internals.h
    \brief This file contains the internal definitions for the mem audit library
*/


#ifndef OSCL_MEM_AUDIT_INTERNALS_H
#define OSCL_MEM_AUDIT_INTERNALS_H


#include "oscl_base.h"
#include "oscl_mem_audit.h"
#include "oscl_mem_inst.h"

#define OSCL_DISABLE_WARNING_TRUNCATE_DEBUG_MESSAGE
#include "osclconfig_compiler_warnings.h"

#if(PVMEM_INST_LEVEL>0)
#define MM_AUDIT_ALLOC_NODE_SUPPORT 1
#define MM_AUDIT_FENCE_SUPPORT 1
#define MM_AUDIT_INCLUDE_ALL_HEAP_VALIDATION 1
#define MM_AUDIT_FILL_SUPPORT 1
#define MM_AUDIT_FAILURE_SIMULATION_SUPPORT 1
#else
#define MM_AUDIT_ALLOC_NODE_SUPPORT 1
#define MM_AUDIT_FENCE_SUPPORT 0
#define MM_AUDIT_INCLUDE_ALL_HEAP_VALIDATION 1
#define MM_AUDIT_FILL_SUPPORT 0
#define MM_AUDIT_FAILURE_SIMULATION_SUPPORT 1
#endif

#define FENCE_PATTERN 0xAA
#define MIN_FENCE_SIZE 4

#ifdef MEM_ALIGN_4
#define MEM_ALIGN_SIZE 4
#else
#define MEM_ALIGN_SIZE 8
#endif



/* CURRENTLY THIS STRUCTURE IS A MULTIPLE OF 8 BYTES.  MAKE SURE IT
 * MAINTAINS A SIZE THAT IS AN INTEGER MULTIPLE OF THE ALIGNMENT SIZE
 * FOR THE PLATFORM IN QUESTION.  ADD PAD BYTES TO THE STRUCTURE
 * IF NECESSARY
 */
struct MM_AllocBlockHdr
{
    void *pNode; //pointer to either a stats node or an alloc node.
    uint32 size;

    void *pRootNode; //pointer to root node, to use when de-allocating.
    uint32 pad;//to maintain 8-byte alignment

    /* windows compiler assumes the the assignment
     * is a pure-virtual declaration and does not allow
     * it to be non-zero.  We actually set the value below.
     */
    static const uint32 ALLOC_NODE_FLAG; // = 0x80000000;

    bool isAllocNodePtr()
    {
        return ((size & ALLOC_NODE_FLAG) != 0);
    };
    void setAllocNodeFlag()
    {
        size |= ALLOC_NODE_FLAG;
    };

    MM_AllocBlockHdr(): pNode(0), size(0), pRootNode(0) {};
    MM_AllocBlockHdr(void* ptr, uint32 inSize): pNode(ptr), size(inSize) {};
};

// actually set the value here.
const uint32 MM_AllocBlockHdr::ALLOC_NODE_FLAG = 0x80000000;

#define COMPUTE_MEM_ALIGN_SIZE(x,y,z) (y+(((x+y)%z) ? (z - (x+y)%z) : 0))

struct MM_AllocBlockFence
{

    uint8 pad[COMPUTE_MEM_ALIGN_SIZE(sizeof(MM_AllocBlockHdr), MIN_FENCE_SIZE, MEM_ALIGN_SIZE)];
    MM_AllocBlockFence()
    {
        fill_fence();
    };

    void fill_fence()
    {
        oscl_memset(pad, FENCE_PATTERN, sizeof(pad));
    };

    bool check_fence()
    {
        for (uint ii = 0; ii < sizeof(pad); ++ii)
        {
            if (pad[ii] != FENCE_PATTERN) return false;
        }
        return true;
    };
};




#define DEFAULT_PREFILL_PATTERN 0x96
#define DEFAULT_POSTFILL_PATTERN 0x5A





#endif

/*! @} */
